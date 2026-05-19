import api from "./api";

export const checkIn = (qrCode, locationId, zoneId) =>
  api.post("/checkins/qr", { qrCode, locationId, zoneId });

export const getMemberCheckIns = (memberId) =>
  api.get(`/checkins/member/${memberId}`);