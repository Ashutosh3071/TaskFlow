import { Directive, Input, TemplateRef, ViewContainerRef, inject } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { Role } from '../../models/auth.model';

@Directive({
  standalone: true,
  selector: '[appHasRole]'
})
export class HasRoleDirective {
  #tpl = inject(TemplateRef<any>);
  #vcr = inject(ViewContainerRef);
  #auth = inject(AuthService);

  private roles: Role[] = [];

  @Input('appHasRole')
  set appHasRole(value: Role[] | Role) {
    this.roles = Array.isArray(value) ? value : [value];
    this.updateView();
  }

  private updateView(): void {
    const current = this.#auth.getCurrentRole();
    const show = current && this.roles.includes(current as Role);
    this.#vcr.clear();
    if (show) {
      this.#vcr.createEmbeddedView(this.#tpl);
    }
  }
}

