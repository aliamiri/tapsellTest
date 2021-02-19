FROM cassandra

COPY entrypoint.sh /entrypoint.sh
EXPOSE 9042
RUN ["chmod", "+x", "/entrypoint.sh"]
ENTRYPOINT ["/entrypoint.sh"]
CMD ["cassandra", "-f"]
