import { Component, input } from '@angular/core';
import { LucideAngularModule, Tag } from 'lucide-angular';
import { TagResponse } from '../../services/url-shortener.service';

@Component({
  selector: 'app-tag-chips',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './tag-chips.html',
  styleUrl: './tag-chips.css'
})
export class TagChipsComponent {
  readonly tags = input.required<TagResponse[]>();

  protected readonly tagIcon = Tag;
}
