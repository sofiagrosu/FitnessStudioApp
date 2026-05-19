import api from "./api";

export const getTrainerCourses   = (trainerId)            => api.get(`/trainer/${trainerId}/courses`);
export const getTrainerSchedule  = (trainerId)            => api.get(`/trainer/${trainerId}/schedule`);
export const getEnrolledCount    = (trainerId, courseId)  => api.get(`/trainer/${trainerId}/courses/${courseId}/enrolled`);
export const getEnrolledSignUps  = (trainerId, courseId)  => api.get(`/trainer/${trainerId}/courses/${courseId}/signups`);
export const setAttendance       = (signUpId, attended)   => api.patch(`/courses/signups/${signUpId}/attendance`, { attended });
