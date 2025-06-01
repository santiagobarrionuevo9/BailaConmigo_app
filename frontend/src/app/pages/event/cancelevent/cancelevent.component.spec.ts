import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CanceleventComponent } from './cancelevent.component';

describe('CanceleventComponent', () => {
  let component: CanceleventComponent;
  let fixture: ComponentFixture<CanceleventComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CanceleventComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CanceleventComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
