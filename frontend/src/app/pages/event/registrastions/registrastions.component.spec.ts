import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegistrastionsComponent } from './registrastions.component';

describe('RegistrastionsComponent', () => {
  let component: RegistrastionsComponent;
  let fixture: ComponentFixture<RegistrastionsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegistrastionsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegistrastionsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
