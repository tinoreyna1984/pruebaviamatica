import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MessageSnackBarComponent } from 'src/app/shared/components/message-snack-bar/message-snack-bar.component';
import Swal from 'sweetalert2';
import { MainService } from '../../services/main.service';
import { User } from 'src/app/shared/interfaces/user.interface';

@Component({
  selector: 'app-delete-usuario',
  templateUrl: './delete-usuario.component.html',
  styleUrls: ['./delete-usuario.component.css']
})
export class DeleteUsuarioComponent {
  constructor(
    @Inject(MAT_DIALOG_DATA) private userID: string,
    private mainService: MainService,
    private snackBar: MatSnackBar
  ) {}

  usuario?: User;

  onBorrarUsuario() {
    this.mainService.borrarUser(this.userID).subscribe(
      {
        next: (response: any) => {
          this.snackBar.openFromComponent(MessageSnackBarComponent, {
            duration: 3500,
            data: response.mensaje,
          });
        },
        error: (e:any) => {
          //console.error(e.message);
          Swal.fire('Error al borrar usuario', "Razón: " + e.message + ". Consulta con el administrador, por favor.", 'error' );
        }
      }
      
    );
  }
}
