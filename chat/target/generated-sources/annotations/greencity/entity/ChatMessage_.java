package greencity.entity;

import java.time.ZonedDateTime;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(ChatMessage.class)
public abstract class ChatMessage_ {

	public static volatile SingularAttribute<ChatMessage, String> imageName;
	public static volatile SingularAttribute<ChatMessage, Participant> sender;
	public static volatile SingularAttribute<ChatMessage, Long> id;
	public static volatile SingularAttribute<ChatMessage, ChatRoom> room;
	public static volatile SingularAttribute<ChatMessage, String> content;
	public static volatile SingularAttribute<ChatMessage, String> fileType;
	public static volatile SingularAttribute<ChatMessage, ZonedDateTime> createDate;

	public static final String IMAGE_NAME = "imageName";
	public static final String SENDER = "sender";
	public static final String ID = "id";
	public static final String ROOM = "room";
	public static final String CONTENT = "content";
	public static final String FILE_TYPE = "fileType";
	public static final String CREATE_DATE = "createDate";

}

