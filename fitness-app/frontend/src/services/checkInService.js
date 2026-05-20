import api from "./api";

export const checkIn              = (qrCode, locationId) =>
  api.post("/checkins/qr", { qrCode, locationId });

export const checkOut             = (checkInId)  => api.put(`/checkins/${checkInId}/checkout`);
export const getMemberCheckIns    = (memberId)   => api.get(`/checkins/member/${memberId}`);
export const getOpenCheckIns      = (locationId) => api.get(`/checkins/location/${locationId}/open`);
export const getOccupancy         = (locationId) => api.get(`/checkins/location/${locationId}/occupancy`);