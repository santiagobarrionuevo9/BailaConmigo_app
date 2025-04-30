import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DancerComponent } from './dancer.component';

describe('DancerComponent', () => {
  let component: DancerComponent;
  let fixture: ComponentFixture<DancerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DancerComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DancerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
