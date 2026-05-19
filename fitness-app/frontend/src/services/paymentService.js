import api from "./api";

export const getPayments = (memberId) =>
  api.get(`/payments/member/${memberId}`);