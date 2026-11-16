export type Role = "ADMIN" | "DOCENTE" | "ESTUDIANTE";
export interface Person {
  id: number;
  fullName: string;
  initials: string;
  role: Role;
  professionalTitle: string;
}
export interface Account extends Person {
  email: string;
  phone: string;
  active: boolean;
}
export interface Course {
  id: number;
  title: string;
  description: string;
  objectives: string;
  category: string;
  level: string;
  durationHours: number;
  price: number;
  published: boolean;
  certificateEnabled: boolean;
  teacherNames: string;
  teacherIds: number[];
  teachers: Person[];
}
export interface AcademicModule {
  id: number;
  title: string;
  position: number;
  courseId: number;
}
export type ContentType =
  "TEXTO" | "VIDEO" | "MATERIAL" | "ACTIVIDAD" | "FORO" | "EVALUACION";
export interface Content {
  id: number;
  title: string;
  moduleId: number;
  moduleTitle: string;
  courseId: number;
  courseTitle: string;
  type: ContentType;
  position: number;
  preview: boolean;
  maxAttempts: number;
  passPercent: number;
  examActive: boolean;
  body?: string;
  resourceUrl?: string;
  fileName?: string;
  hasFile?: boolean;
}
export interface SyllabusRow {
  module: AcademicModule;
  contents: Content[];
}
export interface Enrollment {
  id: number;
  student: Account;
  course: Course;
  startsAt: string;
  expiresAt: string;
  status: string;
  current: boolean;
}
export interface CourseRow {
  course: Course;
  enrollment: Enrollment;
  percent: number;
  nextContent: Content | null;
  moduleCount: number;
  contentCount: number;
}
export interface Payment {
  id: number;
  version: number;
  student: Account;
  course: Course;
  amount: number;
  reference: string;
  status: string;
  createdAt: string;
  expiresAt: string | null;
}
export interface Submission {
  id: number;
  student: Account;
  content: Content;
  attemptNumber: number;
  fileName: string;
  comment: string;
  grade: number | null;
  feedback: string;
  createdAt: string;
}
export type QuestionType =
  | "OPCION_MULTIPLE"
  | "VERDADERO_FALSO"
  | "RELACIONAR"
  | "COMPLETAR"
  | "SELECCIONAR_ESPACIOS";
export interface Question {
  id: number;
  type: QuestionType;
  prompt: string;
  points: number;
  position: number;
  options: string[];
  labels: string[];
  choices: string[];
  gapCount: number;
  optionsText?: string;
  correctAnswer?: string;
  explanation?: string;
}
export interface Attempt {
  id: number;
  student: Account;
  content: Content;
  attemptNumber: number;
  percent: number;
  stars: number;
  passed: boolean;
  createdAt: string;
}
export interface Answer {
  id: number;
  question: Question;
  answerText: string;
  correct: boolean;
  earnedPoints: number;
}
export interface Post {
  id: number;
  author: Person;
  content: Content;
  parentId: number | null;
  parentAuthor: string | null;
  body: string;
  deleted: boolean;
  createdAt: string;
}
export interface PostRow {
  post: Post;
  reactionCount: number;
  rating: string;
}
export interface Review {
  id: number;
  author: Person;
  body: string;
  score: number;
}
export interface Certificate {
  id: number;
  courseId: number;
  studentName: string;
  courseTitle: string;
  code: string;
  issuedAt: string;
}
export interface Session {
  user: Account | null;
  cartCount: number;
  demo: boolean;
}
export interface Catalog {
  courses: Course[];
  continueCourses: Course[];
  featuredCourses: Course[];
  academicRoutes: {
    slug: string;
    title: string;
    label: string;
    description: string;
  }[];
  categories: string[];
  levels: string[];
  accessCounts: Record<string, number>;
  courseInfo: Record<string, { status: string; modules: number }>;
  search: string;
  ruta: string;
  categoria: string;
  nivel: string;
  acceso: string;
}
export interface CourseDetail {
  course: Course;
  syllabus: SyllabusRow[];
  canLearn: boolean;
  canManage: boolean;
  reviews: Review[];
}
export interface Classroom {
  course: Course;
  syllabus: SyllabusRow[];
  completed: number[];
  percent: number;
  canManage: boolean;
}
export interface Lesson extends Classroom {
  content: Content;
  canLearn: boolean;
  posts: PostRow[];
  submissions?: Submission[];
  questions?: Question[];
  attempts?: Attempt[];
}
export interface Dashboard {
  courseCount: number;
  teacherCount: number;
  users: number;
  paymentCount: number;
  paymentTotal: number;
  activeCount: number;
  completedCount: number;
  certificateCount: number;
  totalStars: number;
  courses: Course[];
  myCourses: CourseRow[];
  courseRows: {
    course: Course;
    enrollments: number;
    payments: number;
    certificates: number;
    comments: number;
  }[];
}
export interface Report {
  course: Course;
  roster: { student: Account; enrollment: Enrollment; percent: number }[];
  submissions: Submission[];
  attempts: Attempt[];
}
export interface Options {
  teachers: Account[];
  students: Account[];
  courses: Course[];
  expiration: string;
}
export interface UserSession {
  id: number;
  account: Account;
  roleAtLogin: string;
  loggedInAt: string;
  loggedOutAt: string | null;
  duration: string;
  current: boolean;
}
export interface Audit {
  id: number;
  actor: Account;
  action: string;
  details: string;
  createdAt: string;
}
export interface ApiError {
  status: number;
  message: string;
  errors: Record<string, string>;
}
