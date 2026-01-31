import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Header } from './header/header';
import { Footer } from './footer/footer';
import { Toast } from './shared/toast/toast';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, Header, Footer, Toast],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {}
