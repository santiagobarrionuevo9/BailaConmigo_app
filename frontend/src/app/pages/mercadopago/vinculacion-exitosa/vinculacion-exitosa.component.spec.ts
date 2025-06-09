import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VinculacionExitosaComponent } from './vinculacion-exitosa.component';

describe('VinculacionExitosaComponent', () => {
  let component: VinculacionExitosaComponent;
  let fixture: ComponentFixture<VinculacionExitosaComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VinculacionExitosaComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VinculacionExitosaComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
