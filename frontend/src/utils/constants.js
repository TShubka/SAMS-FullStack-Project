/** Shared constants. Keeping these in one place stops role strings being retyped
 *  (and mistyped) across components. */

export const ROLES = {
  ADMIN: 'ROLE_ADMIN',
  TEACHER: 'ROLE_TEACHER',
  STUDENT: 'ROLE_STUDENT',
  USER: 'ROLE_USER',
};

export const STORAGE_KEYS = {
  TOKEN: 'sams_token',
  USER: 'sams_user',
};

export const ATTENDANCE_STATUS = {
  PRESENT: 'PRESENT',
  ABSENT: 'ABSENT',
  LATE: 'LATE',
};

export const ASSESSMENT_TYPES = ['ASSIGNMENT', 'QUIZ', 'MIDTERM', 'FINAL'];

/** Must stay in sync with app.attendance.threshold in application.yml. */
export const LOW_ATTENDANCE_THRESHOLD = 75;
