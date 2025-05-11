import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyMatchComponent } from './my-match.component';

describe('MyMatchComponent', () => {
  let component: MyMatchComponent;
  let fixture: ComponentFixture<MyMatchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyMatchComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyMatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
