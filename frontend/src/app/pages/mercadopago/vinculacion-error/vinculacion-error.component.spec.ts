import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VinculacionErrorComponent } from './vinculacion-error.component';

describe('VinculacionErrorComponent', () => {
  let component: VinculacionErrorComponent;
  let fixture: ComponentFixture<VinculacionErrorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VinculacionErrorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VinculacionErrorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
