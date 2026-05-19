import api from "./api";

export const getAllUsers        = ()        => api.get("/admin/users");
export const getAllActiveUsers  = ()        => api.get("/admin/users/active");
export const addUser            = (user)    => api.post("/admin/users", user);
export const deactivateUser     = (id)      => api.delete(`/admin/users/${id}`);

export const getAllTrainers      = ()        => api.get("/admin/trainers");
export const getTrainerById      = (id)      => api.get(`/admin/trainers/${id}`);
export const updateTrainer       = (id, t)   => api.put(`/admin/trainers/${id}`, t);

export const getAllReceptionists = ()        => api.get("/admin/receptionists");
export const getReceptionistById = (id)      => api.get(`/admin/receptionists/${id}`);
export const updateReceptionist  = (id, r)   => api.put(`/admin/receptionists/${id}`, r);
