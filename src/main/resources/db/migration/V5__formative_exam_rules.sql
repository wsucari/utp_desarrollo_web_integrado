alter table contents add column exam_active boolean not null default true;
alter table contents drop constraint ck_content_attempts;
alter table contents add constraint ck_content_attempts check (
 (type = 'EVALUACION' and max_attempts between 0 and 20)
 or (type <> 'EVALUACION' and max_attempts between 1 and 10));
alter table contents drop constraint ck_content_pass;
alter table contents add constraint ck_content_pass check (pass_percent between 0 and 100);
alter table exam_attempts drop constraint ck_exam_number;
alter table exam_attempts add constraint ck_exam_number check (attempt_number >= 1);
