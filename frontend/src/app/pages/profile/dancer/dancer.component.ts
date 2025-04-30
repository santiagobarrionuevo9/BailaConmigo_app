import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { ProfileService } from '../../../services/profile.service';
import { EditDancerProfileDto } from '../../../models/editdancerprofile';
import { CommonModule } from '@angular/common';
import { DancerProfileResponseDto } from '../../../models/dancerprofileresponse';

@Component({
  selector: 'app-dancer',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dancer.component.html',
  styleUrl: './dancer.component.css'
})
export class DancerComponent implements OnInit {
  form!: FormGroup;
  userId = 1;
  profileData!: DancerProfileResponseDto;
  isEditing = false;

  constructor(private fb: FormBuilder, private profileService: ProfileService) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      city: [''],
      danceStyles: [[]],
      level: [''],
      aboutMe: [''],
      availability: [''],
      mediaUrls: [[]]
    });

    this.profileService.getProfile(this.userId).subscribe(profile => {
      this.profileData = profile;
      this.form.patchValue(profile);
    });
  }

  toggleEdit(): void {
    this.isEditing = !this.isEditing;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    const dto: EditDancerProfileDto = this.form.getRawValue();
    this.profileService.updateProfile(this.userId, dto).subscribe(() => {
      alert('Perfil actualizado');
      this.isEditing = false;
    });
  }

  addMedia(url: string): void {
    const current = this.form.value.mediaUrls || [];
    if (!current.includes(url)) {
      this.form.patchValue({ mediaUrls: [...current, url] });
    }
  }
  

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (file) {
      const formData = new FormData();
      formData.append('file', file);
  
      this.profileService.uploadMedia(formData).subscribe((url: string) => {
        const currentUrls = this.form.value.mediaUrls || [];
        this.form.patchValue({ mediaUrls: [...currentUrls, url] });
      });
    }
  }
  isVideo(url: string): boolean {
    return /\.(mp4|mov|avi|webm|ogg)$/i.test(url);
  }
  
  isImage(url: string): boolean {
    return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
  }
  
}