import api from "./api";

export async function loginUser(email, password) {
  const res = await api.post("/auth/login", { email, password });
  localStorage.setItem("user", JSON.stringify(res.data));
  return res.data;
}

export async function registerUser(member) {
  const res = await api.post("/auth/register", member);
  localStorage.setItem("user", JSON.stringify(res.data));
  return res.data;
}

export function getCurrentUser() {
  const user = localStorage.getItem("user");
  return user ? JSON.parse(user) : null;
}

export function logoutUser() {
  localStorage.removeItem("user");
}