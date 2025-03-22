-- NPC 데이터 삽입 (ON DUPLICATE KEY UPDATE 대신 ON CONFLICT 사용)
INSERT INTO npc (npc_id, npc_name) VALUES (1, '어린왕자') ON CONFLICT (npc_id) DO UPDATE SET npc_name = EXCLUDED.npc_name;
INSERT INTO npc (npc_id, npc_name) VALUES (2, '장미') ON CONFLICT (npc_id) DO UPDATE SET npc_name = EXCLUDED.npc_name;
INSERT INTO npc (npc_id, npc_name) VALUES (3, '여우') ON CONFLICT (npc_id) DO UPDATE SET npc_name = EXCLUDED.npc_name;
INSERT INTO npc (npc_id, npc_name) VALUES (4, '바오밥') ON CONFLICT (npc_id) DO UPDATE SET npc_name = EXCLUDED.npc_name;
INSERT INTO npc (npc_id, npc_name) VALUES (5, 'system') ON CONFLICT (npc_id) DO UPDATE SET npc_name = EXCLUDED.npc_name;

-- 대화 데이터 삽입
INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES ('tutorial', 1, '{userName}님, 저희가 각각 관리하는 감정의 별이 흩어져 있는 상태입니다.$n단원들에게 어떤 별을 전달 해 줘야 하는 지 제가 알려드리겠습니다.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('click_pride',1, '{userName}님, 이 별은 교만의 별입니다. 제가 관리하는 별 중 하나에요.$n이제 다른 별을 찾아보죠.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('click_envy',1,'이 별은 질투의 별이네요! 관리자는 ''장미''라는 단원입니다. 이름이 저희 해결단이랑 똑같죠?$n그 친구 이름을 본 딴 거랍니다.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('deliver_envy',2,'.....$n고마워.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('deliver_envy',1,'장미는 특별한 친구죠.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('click_lonely',1,'{userName}님, 외로움의 별을 찾으셨군요! 이 별의 관리자는 우리의 부단장 ''바오밥''입니다.$n굉장히 든든한 친구죠.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('deliver_lonely',4,'고맙군. 누가 난장을 피운 이 광경을 의뢰인이 목격한 지금 할 말은 없지만...$n어린왕자, 이 분 의뢰비는 좀 깎아드리도록.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('deliver_lonely',1,'아하하...');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('click_sad',1,'어, 이 별은 슬픔의 별인데요... 전혀 어울리지 않는 친구가 관리합니다.$n마지막 남은 단원이니 보면 아실 거에요.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('deliver_sad', 1, '바로 ''여우''입니다.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('deliver_sad',3,'와! 내 별이다! 정말 정말 고마워! 오늘 오기로 한 의뢰인이 당신이구나?$n어떤 의뢰인진 모르겠지만 이 별은 당신을 위해 사용할게.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('quest_end',1,'이제 얼추 정리가 된 것 같군요. 다시 한 번 ''장미''의 단정으로서 감사드립니다.$n그렇다면 이제 {userName}님의 의뢰를 받아볼까요?');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('pick_npc',1,'다음으론 저희 4명 중에서 의뢰를 맡기고 싶은 해결사를 선택해주시면 됩니다.$n{userName}님이 전달해주신 별들은, 해결사 각자의 힘을 이용해 새로운 별로 탄생할 거에요.');

INSERT INTO dialogue (dialogue_type, npc_id, dialogue_text)
VALUES('game_clear',5,'의뢰가 접수되었습니다!$n{userName}님이 작성해주신 주소로 의뢰 접수 확인서와 선물을 보냈습니다.');