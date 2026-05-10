import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '1m', target: 20 },
        { duration: '5m', target: 20 },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
    },
};

export default function () {
    const baseUrl = 'http://localhost:8081/links';

    const userId = Math.floor(Math.random() * 1000) + 1;

    const params = {
        headers: {
            'Tg-Chat-Id': userId.toString(),
            'Content-Type': 'application/json',
        },
    };

    const actionChance = Math.random();

    if (actionChance < 0.01) {
        const payload = JSON.stringify({
            link: `https://github.com/loadtest/test_${Math.random()}`,
            tags: ["test"]
        });

        const postRes = http.post(baseUrl, payload, params);
        check(postRes, { 'POST status is 200': (r) => r.status === 200 });

    } else {
        const getRes = http.get(baseUrl, params);
        check(getRes, { 'GET status is 200': (r) => r.status === 200 });
    }

    sleep(0.1);
}
