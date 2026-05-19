import api from "./api";

export const getActiveSubscription = (memberId) =>
  api.get(`/subscriptions/member/${memberId}/active`);

export const createSubscription = (memberId, type, price) =>
  api.post("/subscriptions", { memberId, type, price });