import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DashboardFinanceComponent } from './dashboard-finance.component';

describe('DashboardFinanceComponent', () => {
  let component: DashboardFinanceComponent;
  let fixture: ComponentFixture<DashboardFinanceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardFinanceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DashboardFinanceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
