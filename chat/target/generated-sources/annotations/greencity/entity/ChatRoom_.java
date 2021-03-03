package greencity.entity;

import greencity.enums.ChatType;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ChatRoom.class)
public abstract class ChatRoom_ {

	public static volatile SingularAttribute<ChatRoom, Participant> owner;
	public static volatile SingularAttribute<ChatRoom, String> name;
	public static volatile ListAttribute<ChatRoom, ChatMessage> messages;
	public static volatile SingularAttribute<ChatRoom, Long> id;
	public static volatile SingularAttribute<ChatRoom, ChatType> type;
	public static volatile SetAttribute<ChatRoom, Participant> participants;

	public static final String OWNER = "owner";
	public static final String NAME = "name";
	public static final String MESSAGES = "messages";
	public static final String ID = "id";
	public static final String TYPE = "type";
	public static final String PARTICIPANTS = "participants";

}

