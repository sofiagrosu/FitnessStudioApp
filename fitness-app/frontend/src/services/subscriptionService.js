import api from "./api";

export const getActiveSubscription = (memberId) =>
  api.get(`/subscriptions/member/${memberId}/active`);

export const createSubscription = (memberId, type, price) =>
  api.post("/subscriptions", { memberId, type, price });

export const cancelSubscription = (subscriptionId) =>
  api.put(`/subscriptions/${subscriptionId}/suspend`);