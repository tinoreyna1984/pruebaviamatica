import { Component, inject } from '@angular/core';
import { FormGroup, FormControl } from '@angular/forms';
import { MainService } from '../../services/main.service';
import Swal from 'sweetalert2';
import { MessageSnackBarComponent } from 'src/app/shared/components/message-snack-bar/message-snack-bar.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-add-usuario',
  templateUrl: './add-usuario.component.html',
  styleUrls: ['./add-usuario.component.css']
})
export class AddUsuarioComponent {

  private mainService = inject(MainService);
  private snackBar = inject(MatSnackBar);

  formAddUsuario: FormGroup;

  constructor() {
    this.formAddUsuario = new FormGroup({
      name: new FormControl(),
      lastName: new FormControl(),
      username: new FormControl(),
      password: new FormControl(),
      accessId: new FormControl(),
      role: new FormControl(),
    });
  }

  onAddUsuario(){
    let tmpForm: any = this.formAddUsuario.value;
    const { role } = this.formAddUsuario.value;
    if(!role)
      tmpForm = {...tmpForm, role: 'USER'};
    //console.log(tmpForm);
    this.mainService.addUser(tmpForm).subscribe(
      {
        next: (response: any) => {
          //console.log(response);
          this.snackBar.openFromComponent(MessageSnackBarComponent, {
            duration: 3500,
            data: response.mensaje,
          });
        },
        error: (e:any) => {
          console.error(e.message);
          Swal.fire('Error al agregar usuario', "Razón: " + e.message + ". Consulta con el administrador, por favor.", 'error' );
        }
      }
    )
  }

}
