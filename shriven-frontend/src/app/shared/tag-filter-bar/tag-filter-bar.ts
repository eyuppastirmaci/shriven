import { Component, input, output } from '@angular/core';
import { LucideAngularModule, Tag } from 'lucide-angular';
import { TagResponse } from '../../services/url-shortener.service';

@Component({
  selector: 'app-tag-filter-bar',
  standalone: true,
  imports: [LucideAngularModule],
  templateUrl: './tag-filter-bar.html',
  styleUrl: './tag-filter-bar.css'
})
export class TagFilterBarComponent {
  readonly tags = input.required<TagResponse[]>();
  readonly selectedTagId = input<number | null>(null);

  readonly tagFilterChange = output<number | null>();

  protected readonly tagIcon = Tag;

  onSelectAll(): void {
    this.tagFilterChange.emit(null);
  }

  onSelectTag(tagId: number): void {
    this.tagFilterChange.emit(tagId);
  }
}
