import { Routes } from '@angular/router';
import { AuthGuard } from './core/services/auth-guard.service';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/player-login/player-login.component').then(m => m.PlayerLoginComponent) },
  { path: 'register', loadComponent: () => import('./features/player-register/player-register.component').then(m => m.PlayerRegisterComponent) },
  { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard-overview.component').then(m => m.DashboardOverviewComponent), canActivate: [AuthGuard] },
  { path: 'contracts', loadComponent: () => import('./features/contracts/contracts-list.component').then(m => m.ContractsListComponent), canActivate: [AuthGuard] },
  { path: 'contracts/:id', loadComponent: () => import('./features/contracts/contract-detail.component').then(m => m.ContractDetailComponent), canActivate: [AuthGuard] },
  { path: 'crew', loadComponent: () => import('./features/crew/crew-list.component').then(m => m.CrewListComponent), canActivate: [AuthGuard] },
  { path: 'crew/:id', loadComponent: () => import('./features/crew/crew-profile.component').then(m => m.CrewProfileComponent), canActivate: [AuthGuard] },
  { path: 'finance', loadComponent: () => import('./features/finance/finance.component').then(m => m.FinanceComponent), canActivate: [AuthGuard] },
  { path: 'ai-actions', loadComponent: () => import('./features/ai-actions/ai-actions.component').then(m => m.AiActionsComponent), canActivate: [AuthGuard] },
  { path: 'role', loadComponent: () => import('./features/role-switch/role-switch.component').then(m => m.RoleSwitchComponent), canActivate: [AuthGuard] },
  { path: 'character', loadComponent: () => import('./features/character/character-overview.component').then(m => m.CharacterOverviewComponent), canActivate: [AuthGuard] },
  { path: 'character/contracts', loadComponent: () => import('./features/character/character-contracts.component').then(m => m.CharacterContractsComponent), canActivate: [AuthGuard] },
  { path: 'character/stats', loadComponent: () => import('./features/character/character-stats.component').then(m => m.CharacterStatsComponent), canActivate: [AuthGuard] },
  { path: '**', redirectTo: '' },
];
