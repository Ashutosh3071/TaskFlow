export type Role = 'ADMIN' | 'MANAGER' | 'MEMBER' | 'VIEWER';

export interface AuthRequest { email: string; password: string; }
export interface RegisterRequest { fullName: string; email: string; password: string; }
export interface AuthResponse {
  token: string;
  userId: number;
  fullName: string;
  email: string;
  role: Role;
}