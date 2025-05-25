#!/bin/bash

# Load test script for comparing ResultMap and Stream approaches

# Configuration
HOST="localhost:8080"
RESULTMAP_ENDPOINT="/api/processors/resultMap"
STREAM_ENDPOINT="/api/processors/stream"
REQUESTS=1000
CONCURRENCY=10
OUTPUT_DIR="load-test-results"

# Create output directory
mkdir -p $OUTPUT_DIR

echo "Starting load tests with $REQUESTS requests and concurrency level $CONCURRENCY"

# Warm-up
echo "Warming up..."
curl -s "http://$HOST$RESULTMAP_ENDPOINT" > /dev/null
curl -s "http://$HOST$STREAM_ENDPOINT" > /dev/null
sleep 2

# Test ResultMap approach
echo "Testing ResultMap approach..."
ab -n $REQUESTS -c $CONCURRENCY -g "$OUTPUT_DIR/resultmap.dat" "http://$HOST$RESULTMAP_ENDPOINT" > "$OUTPUT_DIR/resultmap.txt"

# Wait between tests
sleep 5

# Test Stream approach
echo "Testing Stream approach..."
ab -n $REQUESTS -c $CONCURRENCY -g "$OUTPUT_DIR/stream.dat" "http://$HOST$STREAM_ENDPOINT" > "$OUTPUT_DIR/stream.txt"

# Extract and compare results
echo "Extracting results..."

# ResultMap metrics
RESULTMAP_RPS=$(grep "Requests per second" "$OUTPUT_DIR/resultmap.txt" | awk '{print $4}')
RESULTMAP_MEAN=$(grep "Time per request" "$OUTPUT_DIR/resultmap.txt" | head -1 | awk '{print $4}')
RESULTMAP_P95=$(grep "95%" "$OUTPUT_DIR/resultmap.txt" | awk '{print $2}')

# Stream metrics
STREAM_RPS=$(grep "Requests per second" "$OUTPUT_DIR/stream.txt" | awk '{print $4}')
STREAM_MEAN=$(grep "Time per request" "$OUTPUT_DIR/stream.txt" | head -1 | awk '{print $4}')
STREAM_P95=$(grep "95%" "$OUTPUT_DIR/stream.txt" | awk '{print $2}')

# Generate comparison report
echo "Generating comparison report..."
cat > "$OUTPUT_DIR/comparison.txt" << EOF
Performance Comparison: ResultMap vs Stream

Test Configuration:
- Requests: $REQUESTS
- Concurrency: $CONCURRENCY

ResultMap Approach:
- Requests per second: $RESULTMAP_RPS
- Mean response time: $RESULTMAP_MEAN ms
- 95th percentile response time: $RESULTMAP_P95 ms

Stream Approach:
- Requests per second: $STREAM_RPS
- Mean response time: $STREAM_MEAN ms
- 95th percentile response time: $STREAM_P95 ms

Performance Difference:
- Requests per second: $(echo "scale=2; ($STREAM_RPS - $RESULTMAP_RPS) / $RESULTMAP_RPS * 100" | bc)% (Stream vs ResultMap)
- Mean response time: $(echo "scale=2; ($STREAM_MEAN - $RESULTMAP_MEAN) / $RESULTMAP_MEAN * 100" | bc)% (Stream vs ResultMap)
- 95th percentile response time: $(echo "scale=2; ($STREAM_P95 - $RESULTMAP_P95) / $RESULTMAP_P95 * 100" | bc)% (Stream vs ResultMap)

EOF

echo "Load tests completed. Results are available in the $OUTPUT_DIR directory."
echo "See $OUTPUT_DIR/comparison.txt for a summary of the results."