export interface User {
  id: number;
  username: string;
  password: string;
  accessId: string;
  email: string;
  name: string;
  lastName: string;
  role: Role;
  enabled: boolean;
  authorities: Authority[];
  accountNonExpired: boolean;
  credentialsNonExpired: boolean;
  accountNonLocked: boolean;
}

export enum Role {
  USER = 'USER',
  ADMINISTRATOR = 'ADMINISTRATOR',
}

export interface Authority {
  authority: string;
}
