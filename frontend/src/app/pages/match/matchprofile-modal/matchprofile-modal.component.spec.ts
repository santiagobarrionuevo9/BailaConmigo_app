import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MatchprofileModalComponent } from './matchprofile-modal.component';

describe('MatchprofileModalComponent', () => {
  let component: MatchprofileModalComponent;
  let fixture: ComponentFixture<MatchprofileModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MatchprofileModalComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MatchprofileModalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
