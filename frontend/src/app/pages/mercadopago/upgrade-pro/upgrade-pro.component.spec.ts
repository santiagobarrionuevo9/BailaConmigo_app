import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpgradeProComponent } from './upgrade-pro.component';

describe('UpgradeProComponent', () => {
  let component: UpgradeProComponent;
  let fixture: ComponentFixture<UpgradeProComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpgradeProComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpgradeProComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
