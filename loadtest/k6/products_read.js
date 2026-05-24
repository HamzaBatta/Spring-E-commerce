import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8081";
const ORDER_PATH = __ENV.ORDER_PATH || "/orders";
const STRATEGY = __ENV.STRATEGY || "default";

const USER_ID = __ENV.USER_ID || "1";
const STORAGE_ID = __ENV.STORAGE_ID || "1";
const PRODUCT_ID = __ENV.PRODUCT_ID || "1";
const QUANTITY = __ENV.QUANTITY || "1";

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS, 10) : 100,
  duration: __ENV.DURATION || "2m",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<1000"],
  },
};

export default function () {
  const url = `${BASE_URL}${ORDER_PATH}`;
  const payload = JSON.stringify({
    userId: Number(USER_ID),
    storageId: Number(STORAGE_ID),
    items: [
      {
        productId: Number(PRODUCT_ID),
        quantity: Number(QUANTITY),
      },
    ],
  });

  const params = {
    headers: {
      "Content-Type": "application/json",
      "X-Strategy": STRATEGY,
    },
  };

  const res = http.post(url, payload, params);
  check(res, {
    "status is 201": (r) => r.status === 201,
  });
  sleep(0.1);
}
