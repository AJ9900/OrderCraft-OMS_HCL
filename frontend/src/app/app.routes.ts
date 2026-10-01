import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
	{
		path: 'login',
		loadComponent: () => import('./features/auth/login.component').then(module => module.LoginComponent)
	},
	{
		path: '',
		canActivate: [authGuard],
		loadComponent: () => import('./layout/app-shell.component').then(module => module.AppShellComponent),
		children: [
			{ path: '', pathMatch: 'full', redirectTo: 'dashboard' },
			{
				path: 'dashboard',
				loadComponent: () => import('./features/dashboard/dashboard.component').then(module => module.DashboardComponent)
			},
			{
				path: ':module',
				loadComponent: () => import('./shared/components/resource-page.component').then(module => module.ResourcePageComponent)
			}
		]
	},
	{ path: '**', redirectTo: '' }
];
