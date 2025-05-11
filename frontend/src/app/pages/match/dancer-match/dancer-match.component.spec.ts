import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DancerMatchComponent } from './dancer-match.component';

describe('DancerMatchComponent', () => {
  let component: DancerMatchComponent;
  let fixture: ComponentFixture<DancerMatchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DancerMatchComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DancerMatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
