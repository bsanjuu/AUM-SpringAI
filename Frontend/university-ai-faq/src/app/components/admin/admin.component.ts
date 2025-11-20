import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../services/admin.service';
import { LoadingResponse, IndexingStats } from '../../models/admin.models';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.scss']
})
export class AdminComponent implements OnInit {
  private adminService = inject(AdminService);

  stats = signal<IndexingStats | null>(null);
  isLoading = signal(false);
  loadingMessage = signal('');
  lastLoadResult = signal<LoadingResponse | null>(null);
  customUrls = signal('');
  showCustomUrlInput = signal(false);

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.adminService.getIndexingStats().subscribe({
      next: (stats) => {
        this.stats.set(stats);
      },
      error: (err) => {
        console.error('Error loading stats:', err);
      }
    });
  }

  loadAumData() {
    this.isLoading.set(true);
    this.loadingMessage.set('Loading AUM data from pre-configured URLs...');
    this.lastLoadResult.set(null);

    this.adminService.loadAumData().subscribe({
      next: (response: LoadingResponse) => {
        this.isLoading.set(false);
        this.loadingMessage.set('');
        this.lastLoadResult.set(response);
        this.loadStats(); // Refresh stats
      },
      error: (err) => {
        console.error('Error loading AUM data:', err);
        this.isLoading.set(false);
        this.loadingMessage.set('');
        this.lastLoadResult.set({
          success: false,
          message: 'Failed to load AUM data',
          error: err.message || 'Unknown error'
        });
      }
    });
  }

  loadCustomUrls() {
    const urlsText = this.customUrls().trim();
    if (!urlsText) {
      alert('Please enter at least one URL');
      return;
    }

    const urls = urlsText.split('\n').map(u => u.trim()).filter(u => u);
    if (urls.length === 0) {
      alert('Please enter valid URLs');
      return;
    }

    this.isLoading.set(true);
    this.loadingMessage.set(`Loading data from ${urls.length} custom URL(s)...`);
    this.lastLoadResult.set(null);

    this.adminService.loadFromUrls(urls).subscribe({
      next: (response: LoadingResponse) => {
        this.isLoading.set(false);
        this.loadingMessage.set('');
        this.lastLoadResult.set(response);
        this.loadStats(); // Refresh stats
        this.customUrls.set('');
        this.showCustomUrlInput.set(false);
      },
      error: (err) => {
        console.error('Error loading custom URLs:', err);
        this.isLoading.set(false);
        this.loadingMessage.set('');
        this.lastLoadResult.set({
          success: false,
          message: 'Failed to load custom URLs',
          error: err.message || 'Unknown error'
        });
      }
    });
  }

  reindexAllDocuments() {
    if (!confirm('Are you sure you want to reindex all documents? This may take some time.')) {
      return;
    }

    this.isLoading.set(true);
    this.loadingMessage.set('Reindexing all documents...');
    this.lastLoadResult.set(null);

    this.adminService.reindexAllDocuments().subscribe({
      next: (response: LoadingResponse) => {
        this.isLoading.set(false);
        this.loadingMessage.set('');
        this.lastLoadResult.set(response);
        this.loadStats(); // Refresh stats
      },
      error: (err) => {
        console.error('Error reindexing:', err);
        this.isLoading.set(false);
        this.loadingMessage.set('');
        this.lastLoadResult.set({
          success: false,
          message: 'Failed to reindex documents',
          error: err.message || 'Unknown error'
        });
      }
    });
  }

  toggleCustomUrlInput() {
    this.showCustomUrlInput.update(v => !v);
  }

  getCategoryEntries(): Array<[string, number]> {
    if (!this.stats()?.stats?.documentsByCategory) {
      return [];
    }
    return Object.entries(this.stats()!.stats.documentsByCategory);
  }

  formatDuration(ms: number): string {
    if (ms < 1000) {
      return `${ms}ms`;
    } else if (ms < 60000) {
      return `${(ms / 1000).toFixed(1)}s`;
    } else {
      return `${(ms / 60000).toFixed(1)}min`;
    }
  }
}
