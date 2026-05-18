import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-about-me',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './about-me.html',
  styleUrls: ['./about-me.css']
})
export class AboutMeComponent {

  @Input() isOpen = false;
  @Output() close = new EventEmitter<void>();
  @Input() title = 'Sara Mageste';
  @Input() message = `
    Experiência com desenvolvimento de software desde 2020, atuando principalmente com Java, Spring Boot e APIs RESTful. Atualmente, estou expandindo minha atuação para o front-end com Angular, buscando construir aplicações completas com foco tanto em arquitetura quanto em experiência do usuário.

    Este projeto de gerenciamento de produtos nasceu como uma forma de aprofundar meus conhecimentos em desenvolvimento fullstack através de uma aplicação real, estruturada e escalável.

    Além da parte técnica, também tenho bastante interesse em design de produto e experiência do usuário, então gosto de pensar não apenas no funcionamento da aplicação, mas em como torná-la agradável, intuitiva e visualmente consistente.

    Atualmente, sigo evoluindo o projeto com novas funcionalidades como autenticação, sistema de promoções, notificações e gerenciamento de usuários, enquanto continuo estudando engenharia de software e arquitetura de aplicações.
    `;

}