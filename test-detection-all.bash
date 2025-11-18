#!/usr/bin/env bash
#
# Sample usage:
#
#   HOST=localhost PORT=8086 ./test-detection-all.bash
#
: ${HOST=localhost}
: ${PORT=8086}
: ${SOURCE_ID_VALID="camera-001"}
: ${SOURCE_ID_NOT_FOUND="camera-999"}

function assertCurl() {

  local expectedHttpCode=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE='' && (( ${#result} > 3 )) && RESPONSE="${result%???}"

  if [ "$httpCode" = "$expectedHttpCode" ]
  then
    if [ "$httpCode" = "200" ]
    then
      echo "Test OK (HTTP Code: $httpCode)"
    else
      echo "Test OK (HTTP Code: $httpCode, $RESPONSE)"
    fi
  else
    echo  "Test FAILED, EXPECTED HTTP Code: $expectedHttpCode, GOT: $httpCode, WILL ABORT!"
    echo  "- Failing command: $curlCmd"
    echo  "- Response Body: $RESPONSE"
    exit 1
  fi
}

function assertEqual() {

  local expected=$1
  local actual=$2

  if [ "$actual" = "$expected" ]
  then
    echo "Test OK (actual value: $actual)"
  else
    echo "Test FAILED, EXPECTED VALUE: $expected, ACTUAL VALUE: $actual, WILL ABORT"
    exit 1
  fi
}

function assertNotEmpty() {

  local actual=$1
  local fieldName=$2

  if [ ! -z "$actual" ] && [ "$actual" != "null" ]
  then
    echo "Test OK ($fieldName is not empty: $actual)"
  else
    echo "Test FAILED, EXPECTED $fieldName to have a value, GOT: $actual, WILL ABORT"
    exit 1
  fi
}

function testUrl() {
  url=$@
  if $url -ks -f -o /dev/null
  then
    return 0
  else
    return 1
  fi;
}

function waitForService() {
  url=$@
  echo -n "Wait for: $url... "
  n=0
  until testUrl $url
  do
    n=$((n + 1))
    if [[ $n == 100 ]]
    then
      echo " Give up"
      exit 1
    else
      sleep 3
      echo -n ", retry #$n "
    fi
  done
  echo "DONE, continues..."
}

set -e

echo "Start Detection Composite Tests:" `date`

echo "HOST=${HOST}"
echo "PORT=${PORT}"

if [[ $@ == *"start"* ]]
then
  echo "Restarting the test environment..."
  echo "$ docker compose down --remove-orphans"
  docker compose down --remove-orphans
  echo "$ docker compose up -d"
  docker compose up -d
fi

# Wait for detection-composite service to be ready
echo ""
echo "=== Waiting for Detection Composite Service to start ==="
waitForService curl http://$HOST:$PORT/detection-composite/$SOURCE_ID_VALID

echo ""
echo "=== Testing Detection Composite Service ==="

# Test 1: Verify that composite service aggregates data from both services
echo "Test 1: GET Detection aggregate for valid sourceId"
assertCurl 200 "curl http://$HOST:$PORT/detection-composite/$SOURCE_ID_VALID -s"
assertEqual "$SOURCE_ID_VALID" "$(echo $RESPONSE | jq -r .sourceId)"

# Verify detections data is present
assertNotEmpty "$(echo $RESPONSE | jq -r .detections)" "detections"
assertEqual "$SOURCE_ID_VALID" "$(echo $RESPONSE | jq -r .detections.sourceId)"
assertNotEmpty "$(echo $RESPONSE | jq -r .detections.detections)" "detections array"
assertNotEmpty "$(echo $RESPONSE | jq -r .detections.detections[0].plateNum)" "plateNum"

# Verify journey data is present
assertNotEmpty "$(echo $RESPONSE | jq -r .journey)" "journey"
assertNotEmpty "$(echo $RESPONSE | jq -r .journey.reid)" "journey.reid"
assertNotEmpty "$(echo $RESPONSE | jq -r .journey.timestamp)" "journey.timestamp"
assertEqual "test" "$(echo $RESPONSE | jq -r .journey.hwId)"

# Verify service addresses are present
assertNotEmpty "$(echo $RESPONSE | jq -r .serviceAddresses)" "serviceAddresses"

# Test 2: Verify that composite service returns 404 for empty sourceId
echo "Test 2: GET Detection aggregate with empty sourceId (should return 404)"
assertCurl 404 "curl http://$HOST:$PORT/detection-composite/ -s"

# Test 3: Test multiple different source IDs
echo "Test 3: GET Detection aggregate for different sourceId (camera-002)"
assertCurl 200 "curl http://$HOST:$PORT/detection-composite/camera-002 -s"
assertEqual "camera-002" "$(echo $RESPONSE | jq -r .sourceId)"
assertNotEmpty "$(echo $RESPONSE | jq -r .detections.detections[0].plateNum)" "plateNum in camera-002"

# Test 4: Test with special characters in sourceId
echo "Test 4: GET Detection aggregate with special characters in sourceId"
assertCurl 200 "curl http://$HOST:$PORT/detection-composite/camera-special-123 -s"
assertEqual "camera-special-123" "$(echo $RESPONSE | jq -r .sourceId)"
assertNotEmpty "$(echo $RESPONSE | jq -r .detections)" "detections for special sourceId"

# Test 5: Test with numeric sourceId
echo "Test 5: GET Detection aggregate with numeric sourceId"
assertCurl 200 "curl http://$HOST:$PORT/detection-composite/camera-999 -s"
assertEqual "camera-999" "$(echo $RESPONSE | jq -r .sourceId)"
assertNotEmpty "$(echo $RESPONSE | jq -r .journey)" "journey data"

echo ""
echo "=== All Detection Composite Tests Completed Successfully ==="

if [[ $@ == *"stop"* ]]
then
    echo "We are done, stopping the test environment..."
    echo "$ docker compose down"
    docker compose down
fi

echo "End, all detection composite tests OK:" `date`

