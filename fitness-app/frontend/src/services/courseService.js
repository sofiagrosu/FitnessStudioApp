import api from "./api";

export const getCourses = () => api.get("/courses");
export const joinCourse = (courseId, memberId) =>
  api.post(`/courses/${courseId}/signups`, { memberId });