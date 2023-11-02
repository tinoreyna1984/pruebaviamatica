import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MainPageComponent } from './components/main-page/main-page.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MainRoutingModule } from './main-routing.module';
import { AddUsuarioComponent } from './components/add-usuario/add-usuario.component';
import { DeleteUsuarioComponent } from './components/delete-usuario/delete-usuario.component';
import { ModifyUsuarioComponent } from './components/modify-usuario/modify-usuario.component';
import { MaterialModule } from '../material/material.module';
import { SharedModule } from '../shared/shared.module';



@NgModule({
  declarations: [
    MainPageComponent,
    AddUsuarioComponent,
    DeleteUsuarioComponent,
    ModifyUsuarioComponent
  ],
  imports: [
    CommonModule,
    MainRoutingModule,
    MaterialModule,
    SharedModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class MainModule { }
