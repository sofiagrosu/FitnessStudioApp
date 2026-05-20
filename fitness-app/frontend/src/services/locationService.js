import api from "./api";

export const getLocations = () => api.get("/locations");

export const createLocation = (data) =>
  api.post("/locations", data);

export const updateLocation = (locationId, data) =>
  api.put(`/locations/${locationId}`, data);

export const deleteLocation = (locationId) =>
  api.delete(`/locations/${locationId}`);

export const getLocationOccupancy = (locationId) =>
  api.get(`/checkins/location/${locationId}/occupancy`);