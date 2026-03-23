export interface TeamMember {
  userId: number;
  fullName: string;
  email: string;
  role: 'ADMIN' | 'MANAGER' | 'MEMBER' | 'VIEWER';
  joinedAt: string;
}

export interface Team {
  id: number;
  name: string;
  description?: string | null;
  managerId: number;
  managerName: string;
  managerEmail: string;
  managerRole: 'ADMIN' | 'MANAGER' | 'MEMBER' | 'VIEWER';
  createdAt: string;
  members: TeamMember[];
}

export interface CreateTeamRequest {
  name: string;
  description?: string | null;
  memberIds?: number[];
}

