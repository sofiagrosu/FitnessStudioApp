import api from "./api";

export const getLocations = () => api.get("/locations");

export const getLocationOccupancy = (locationId) =>
  api.get(`/checkins/location/${locationId}/occupancy`);