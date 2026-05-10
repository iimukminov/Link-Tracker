```text
С кешированием (Caffeine + Valkey):

         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/


     execution: local
        script: script.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 7m0s max duration (incl. graceful stop):
              * default: Up to 20 looping VUs for 6m30s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)



  █ THRESHOLDS

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS

    checks_total.......: 65851   168.803443/s
    checks_succeeded...: 100.00% 65851 out of 65851
    checks_failed......: 0.00%   0 out of 65851

    ✓ GET status is 200
    ✓ POST status is 200
    checks_total.......: 65851   168.803443/s
    checks_succeeded...: 100.00% 65851 out of 65851
    checks_failed......: 0.00%   0 out of 65851

    ✓ GET status is 200
    ✓ POST status is 200

    HTTP
    http_req_duration..............: avg=4.4ms    min=506µs    med=2.91ms   max=442.59ms p(90)=6.25ms   p(95)=11.82ms
      { expected_response:true }...: avg=4.4ms    min=506µs    med=2.91ms   max=442.59ms p(90)=6.25ms   p(95)=11.82ms
    http_req_failed................: 0.00%  0 out of 65851
    http_reqs......................: 65851  168.803443/s

    EXECUTION
    iteration_duration.............: avg=105.02ms min=100.54ms med=103.46ms max=573.42ms p(90)=107.11ms p(95)=112.96ms
    iterations.....................: 65851  168.803443/s
    vus............................: 1      min=1          max=20
    vus_max........................: 20     min=20         max=20

    NETWORK
    data_received..................: 524 MB 1.3 MB/s
    data_sent......................: 8.5 MB 22 kB/s




running (6m30.1s), 00/20 VUs, 65851 complete and 0 interrupted iterations


С кешированием (Valkey):

         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/


     execution: local
        script: script.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 7m0s max duration (incl. graceful stop):
              * default: Up to 20 looping VUs for 6m30s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)



  █ THRESHOLDS

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS

    checks_total.......: 64689   165.824646/s
    checks_succeeded...: 100.00% 64689 out of 64689
    checks_failed......: 0.00%   0 out of 64689

    ✓ GET status is 200
    ✓ POST status is 200

    HTTP
    http_req_duration..............: avg=6.08ms  min=560µs    med=4.87ms   max=972.79ms p(90)=8.1ms    p(95)=12.48ms
      { expected_response:true }...: avg=6.08ms  min=560µs    med=4.87ms   max=972.79ms p(90)=8.1ms    p(95)=12.48ms
    http_req_failed................: 0.00%  0 out of 64689
    http_reqs......................: 64689  165.824646/s

    EXECUTION
    iteration_duration.............: avg=106.9ms min=101.07ms med=105.46ms max=1.07s    p(90)=108.89ms p(95)=113.17ms
    iterations.....................: 64689  165.824646/s
    vus............................: 1      min=1          max=20
    vus_max........................: 20     min=20         max=20

    NETWORK
    data_received..................: 514 MB 1.3 MB/s
    data_sent......................: 8.3 MB 21 kB/s



                                                                                                                                                                                                     
running (6m30.1s), 00/20 VUs, 64689 complete and 0 interrupted iterations   

Без кеширования:

         /\      Grafana   /‾‾/
    /\  /  \     |\  __   /  /
   /  \/    \    | |/ /  /   ‾‾\
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/


     execution: local
        script: script.js
        output: -

     scenarios: (100.00%) 1 scenario, 20 max VUs, 7m0s max duration (incl. graceful stop):
              * default: Up to 20 looping VUs for 6m30s over 3 stages (gracefulRampDown: 30s, gracefulStop: 30s)



  █ THRESHOLDS

    http_req_failed
    ✓ 'rate<0.01' rate=0.00%


  █ TOTAL RESULTS

    checks_total.......: 48837   125.196773/s
    checks_succeeded...: 100.00% 48837 out of 48837
    checks_failed......: 0.00%   0 out of 48837

    ✓ GET status is 200
    ✓ POST status is 200

    HTTP
    http_req_duration..............: avg=40.91ms  min=8.03ms   med=40.74ms  max=354.77ms p(90)=61ms     p(95)=64.46ms
      { expected_response:true }...: avg=40.91ms  min=8.03ms   med=40.74ms  max=354.77ms p(90)=61ms     p(95)=64.46ms
    http_req_failed................: 0.00%  0 out of 48837
    http_reqs......................: 48837  125.196773/s

    EXECUTION
    iteration_duration.............: avg=141.61ms min=108.69ms med=141.45ms max=470.18ms p(90)=161.86ms p(95)=165.31ms
    iterations.....................: 48837  125.196773/s
    vus............................: 1      min=1          max=20
    vus_max........................: 20     min=20         max=20

    NETWORK
    data_received..................: 388 MB 995 kB/s
    data_sent......................: 6.3 MB 16 kB/s



                                                                                                                                                                                                     
running (6m30.1s), 00/20 VUs, 48837 complete and 0 interrupted iterations    
```
