CREATE INDEX job_arg_id on job_arg (id);
CREATE INDEX dictionary_text on dictionary (text);
CREATE INDEX dictionary_language on dictionary (language);
CREATE INDEX annotation_mp_idx on annotation (media_package_id);
CREATE INDEX user_action_user_idx on user_action (user_id);
CREATE INDEX user_action_mp_idx on user_action (media_package_id);
