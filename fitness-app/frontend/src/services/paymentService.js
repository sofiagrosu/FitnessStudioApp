import api from "./api";

export const getPayments = (memberId) =>
  api.get(`/payments/member/${memberId}`);

export const registerPayment = (memberId, subscriptionId, amount) =>
  api.post("/payments", {
    memberId,
    subscriptionId,
    amount,
    method: "CARD",
  });