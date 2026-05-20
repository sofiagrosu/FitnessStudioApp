import api from "./api";

export const getCourses = () => api.get("/courses");

export const joinCourse = (courseId, memberId) =>
  api.post(`/courses/${courseId}/signups`, { memberId });

export const getMemberCourses = (memberId) =>
  api.get(`/courses/member/${memberId}`);

export const getCourseSignups = (courseId) =>
  api.get(`/courses/${courseId}/signups`);

export const getCourseWaitlist = (courseId) =>
  api.get(`/courses/${courseId}/waitlist`);

export const leaveCourse = (signUpId, memberId) =>
  api.delete(`/courses/signups/${signUpId}?memberId=${memberId}`);

export const createCourse = (data) => api.post("/courses", data);
export const updateCourse = (courseId, data) => api.put(`/courses/${courseId}`, data);
export const deleteCourse = (courseId) => api.delete(`/courses/${courseId}`);