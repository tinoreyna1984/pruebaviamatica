import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MessageSnackBarComponent } from './components/message-snack-bar/message-snack-bar.component';
import { SpinnerComponent } from './components/spinner/spinner.component';
import { Error404Component } from './pages/error404/error404.component';



@NgModule({
  declarations: [
    MessageSnackBarComponent,
    SpinnerComponent,
    Error404Component
  ],
  imports: [
    CommonModule
  ],
  exports: [
    MessageSnackBarComponent,
    SpinnerComponent,
  ]
})
export class SharedModule { }
