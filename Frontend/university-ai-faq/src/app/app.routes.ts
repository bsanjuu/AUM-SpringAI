import { Routes } from '@angular/router';
import { ChatComponent } from './components/chat/chat.component';
import { AdminComponent } from './components/admin/admin.component';

export const routes: Routes = [
  { path: '', component: ChatComponent },
  { path: 'chat', component: ChatComponent },
  { path: 'admin', component: AdminComponent },
  { path: '**', redirectTo: '' }
];
