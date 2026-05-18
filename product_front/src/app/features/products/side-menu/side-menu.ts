import { Component, Input, ElementRef, ViewChild, HostListener } from '@angular/core';
import { Output, EventEmitter } from '@angular/core';


@Component({
  selector: 'app-side-menu',
  templateUrl: './side-menu.html',
  styleUrls: ['./side-menu.css']
})

export class SideMenuComponent {

    @Input() isOpen = false;
    @Input() userProfile!: {
        name: string;
        employeeCode: string;
        imageUrl: string;
    };

    @Output() closeMenu = new EventEmitter<void>();

    @ViewChild('menuContainer') menuContainer!: ElementRef;

    @HostListener('document:click', ['$event'])
    onDocumentClick(event: MouseEvent): void {

        if (!this.isOpen) return;

        const clickedInside =
            this.menuContainer.nativeElement.contains(event.target);

        if (!clickedInside) {
            this.closeMenu.emit();
        }
    }
        

}